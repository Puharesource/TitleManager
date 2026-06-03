package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_8_R3RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_8_R3", 2) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_8_R3.LegacyV1_8_R3RuntimeVersionModulePacketSink
}
