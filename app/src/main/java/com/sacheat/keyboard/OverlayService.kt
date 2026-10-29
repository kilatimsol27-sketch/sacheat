package com.sacheat.keyboard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private var category = "All"
    private var query = ""
    private val shown = ArrayList<Cheat>()
    private var listAdapter: BaseAdapter? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        if (::wm.isInitialized) {
            hidePanel()
            bubble?.let { try { wm.removeView(it) } catch (_: Exception) {} }
            bubble = null
        }
        super.onDestroy()
    }

    private fun dp(v: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics
    ).toInt()

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CH, "Floating keyboard", NotificationManager.IMPORTANCE_LOW)
        )
        val stop = PendingIntent.getService(
            this, 0,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CH)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("SA Cheat Keyboard")
            .setContentText("Floating button is active")
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel, "Stop", stop
                ).build()
            )
            .setOngoing(true)
            .build()
    }

    private fun params(w: Int, h: Int, flags: Int) = WindowManager.LayoutParams(
        w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, flags, PixelFormat.TRANSLUCENT
    )

    // ---------- Floating bubble ----------
    private fun showBubble() {
        val size = dp(52)
        val tv = TextView(this).apply {
            text = "SA"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#E6D32F2F"))
                setStroke(dp(2), Color.WHITE)
            }
        }
        val lp = params(
            size, size,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(8)
            y = dp(120)
        }
        tv.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0
            var startY = 0
            var touchX = 0f
            var touchY = 0f
            var moved = false

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = lp.x; startY = lp.y
                        touchX = e.rawX; touchY = e.rawY
                        moved = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (e.rawX - touchX).toInt()
                        val dy = (e.rawY - touchY).toInt()
                        if (abs(dx) > dp(6) || abs(dy) > dp(6)) moved = true
                        if (moved) {
                            lp.x = startX + dx
                            lp.y = startY + dy
                            wm.updateViewLayout(v, lp)
                        }
                    }
                    MotionEvent.ACTION_UP -> if (!moved) togglePanel()
                }
                return true
            }
        })
        wm.addView(tv, lp)
        bubble = tv
    }

    // ---------- Cheat panel (modal) ----------
    private fun togglePanel() {
        if (panel == null) showPanel() else hidePanel()
    }

    private fun hidePanel() {
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel = null
    }

    private fun refresh() {
        shown.clear()
        shown.addAll(CHEATS.filter {
            (category == "All" || it.category == category) &&
                (query.isBlank() ||
                    it.code.contains(query, ignoreCase = true) ||
                    it.name.contains(query, ignoreCase = true))
        })
        listAdapter?.notifyDataSetChanged()
    }

    private fun copy(c: Cheat) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("cheat", c.code))
        Toast.makeText(this, "${c.code} copied", Toast.LENGTH_SHORT).show()
    }

    private fun showPanel() {
        category = "All"
        query = ""
        val dm = resources.displayMetrics
        val w = minOf((dm.widthPixels * 0.92f).toInt(), dp(460))
        val h = (dm.heightPixels * 0.82f).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.parseColor("#F2161616"))
            }
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "SA Cheats"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(TextView(this).apply {
            text = "✕"
            setTextColor(Color.WHITE)
            textSize = 22f
            setPadding(dp(12), 0, dp(4), 0)
            setOnClickListener { hidePanel() }
        })
        root.addView(header)

        // Search
        val search = EditText(this).apply {
            hint = "Search code or name"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 14f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(Color.parseColor("#2A2A2A"))
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    query = s?.toString()?.trim() ?: ""
                    refresh()
                }
            })
        }
        root.addView(search, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) })

        // Category chips
        val chips = ArrayList<TextView>()
        fun styleChips() {
            chips.forEach { c ->
                val on = c.text.toString() == category
                c.setTextColor(if (on) Color.BLACK else Color.WHITE)
                (c.background as GradientDrawable)
                    .setColor(Color.parseColor(if (on) "#FFEB3B" else "#333333"))
            }
        }
        val chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (cat in CATEGORIES) {
            val chip = TextView(this).apply {
                text = cat
                textSize = 13f
                setPadding(dp(12), dp(6), dp(12), dp(6))
                background = GradientDrawable().apply { cornerRadius = dp(16).toFloat() }
                setOnClickListener { category = cat; styleChips(); refresh() }
            }
            chips.add(chip)
            chipRow.addView(chip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { rightMargin = dp(6) })
        }
        styleChips()
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(chipRow)
        }
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) })

        // Cheat list
        shown.clear()
        shown.addAll(CHEATS)
        val list = ListView(this).apply {
            divider = ColorDrawable(Color.parseColor("#333333"))
            dividerHeight = dp(1)
        }
        listAdapter = object : BaseAdapter() {
            override fun getCount() = shown.size
            override fun getItem(position: Int) = shown[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val row = (convertView as? LinearLayout) ?: LinearLayout(this@OverlayService).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(8), dp(10), dp(8), dp(10))
                    addView(TextView(context).apply {
                        setTextColor(Color.parseColor("#FFEB3B"))
                        textSize = 18f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    })
                    addView(TextView(context).apply {
                        setTextColor(Color.WHITE)
                        textSize = 13f
                    })
                }
                val c = shown[position]
                (row.getChildAt(0) as TextView).text = c.code
                (row.getChildAt(1) as TextView).text = c.name
                return row
            }
        }
        list.adapter = listAdapter
        list.setOnItemClickListener { _, _, pos, _ -> copy(shown[pos]) }
        root.addView(list, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply { topMargin = dp(6) })

        val lp = params(w, h, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL).apply {
            gravity = Gravity.CENTER
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        }
        wm.addView(root, lp)
        panel = root
    }

    companion object {
        const val ACTION_STOP = "com.sacheat.keyboard.STOP"
        const val CH = "overlay"
    }
}
