package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_8_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_8_R1", 1) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_8_R1.LegacyV1_8_R1RuntimeVersionModulePacketSink
}
