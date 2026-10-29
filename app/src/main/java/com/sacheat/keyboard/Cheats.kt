package com.sacheat.keyboard

data class Cheat(val code: String, val name: String, val category: String)

val CATEGORIES = listOf("All", "Player", "Weapons", "Vehicles", "Weather", "Fun")

// Edit / add cheats here. Format: Cheat("CODE", "Description", "Category")
val CHEATS = listOf(
    // Player
    Cheat("HESOYAM", "Health, armor + \$250,000", "Player"),
    Cheat("AEZAKMI", "Never wanted", "Player"),
    Cheat("OSRBLHH", "Raise wanted level +2", "Player"),
    Cheat("ASNAEB", "Clear wanted level", "Player"),
    Cheat("CVWKXAM", "Infinite lung capacity", "Player"),
    Cheat("BUFFMEUP", "Max muscle", "Player"),
    Cheat("KVGYZQK", "Skinny", "Player"),
    Cheat("BTCDBCB", "Fat", "Player"),
    Cheat("EHIBXQS", "Max sex appeal", "Player"),
    Cheat("YECGAA", "Get a jetpack", "Player"),
    Cheat("AIYPWZQP", "Parachute", "Player"),

    // Weapons
    Cheat("LXGIWYL", "Weapon set 1 (thug's tools)", "Weapons"),
    Cheat("KJKSZPJ", "Weapon set 2 (professional tools)", "Weapons"),
    Cheat("UZUMYMW", "Weapon set 3 (nutter tools)", "Weapons"),
    Cheat("FULLCLIP", "Infinite ammo, no reload", "Weapons"),

    // Vehicles
    Cheat("AIWPRTON", "Spawn Rhino (tank)", "Vehicles"),
    Cheat("OHDUDE", "Spawn Hunter", "Vehicles"),
    Cheat("JUMPJET", "Spawn Hydra", "Vehicles"),
    Cheat("KGGGDKP", "Spawn Vortex hovercraft", "Vehicles"),
    Cheat("AKJJYGLC", "Spawn Quad", "Vehicles"),
    Cheat("JQNTDMH", "Spawn Rancher", "Vehicles"),
    Cheat("URKQSRK", "Spawn Stunt plane", "Vehicles"),
    Cheat("RIPAZHA", "Cars fly", "Vehicles"),
    Cheat("ZEIIVG", "All traffic lights green", "Vehicles"),
    Cheat("IOWDLAC", "Black traffic", "Vehicles"),
    Cheat("LLQPFBN", "Pink traffic", "Vehicles"),
    Cheat("YLTEICZ", "Aggressive drivers", "Vehicles"),

    // Weather / time
    Cheat("ICIKPUS", "Sunny weather", "Weather"),
    Cheat("JHJOECW", "Very sunny weather", "Weather"),
    Cheat("CFVFGMJ", "Overcast weather", "Weather"),
    Cheat("PPGWJHT", "Foggy weather", "Weather"),
    Cheat("CWJXUOC", "Sandstorm", "Weather"),
    Cheat("XJVSNAJ", "Always midnight", "Weather"),
    Cheat("YSOHNUL", "Faster clock", "Weather"),
    Cheat("LIYOAAY", "Slower gameplay", "Weather"),

    // Fun
    Cheat("ASBHGRB", "Elvis is everywhere", "Fun"),
    Cheat("CIKGCGX", "Beach party", "Fun"),
    Cheat("BEKKNQV", "Ninja theme", "Fun"),
    Cheat("BIFBUZZ", "Gang members everywhere", "Fun"),
    Cheat("SJMAHPE", "Recruit anyone (pistol)", "Fun"),
)
