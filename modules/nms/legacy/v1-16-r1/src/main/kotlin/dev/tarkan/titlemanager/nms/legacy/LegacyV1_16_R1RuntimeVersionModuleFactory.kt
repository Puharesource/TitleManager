package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_16_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_16_R1", 10) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_16_R1.LegacyV1_16_R1RuntimeVersionModulePacketSink
}
