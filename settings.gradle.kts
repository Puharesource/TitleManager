rootProject.name = "TitleManager"
include("TitleManagerLib")
include("TitleManagerBukkit")

fun addMinecraftVersionImplementations(vararg versions: String) {
    for (version in versions) {
        include("TitleManagerBukkit:${version}")
        findProject(":TitleManagerBukkit:${version}")?.name = version
    }
}

addMinecraftVersionImplementations(
    "v1_8_R1",
    "v1_8_R2",
    "v1_8_R3",
    "v1_9_R1",
    "v1_9_R2",
    "v1_10_R1",
    "v1_11_R1",
    "v1_12_R1",
    "v1_13_R1",
    "v1_13_R2",
    "v1_14_R1",
    "v1_15_R1",
    "v1_16_R1",
    "v1_16_R2",
    "v1_16_R3",
    "v1_17_R1",
    "v1_18_R1"
)
