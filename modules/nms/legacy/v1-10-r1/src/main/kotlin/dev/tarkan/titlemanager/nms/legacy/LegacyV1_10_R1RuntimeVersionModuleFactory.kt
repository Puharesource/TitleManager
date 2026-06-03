package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_10_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_10_R1", 4) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_10_R1.LegacyV1_10_R1RuntimeVersionModulePacketSink
}
