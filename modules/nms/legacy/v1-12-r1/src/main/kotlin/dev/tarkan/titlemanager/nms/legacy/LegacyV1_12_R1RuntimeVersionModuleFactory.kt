package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_12_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_12_R1", 6) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_12_R1.LegacyV1_12_R1RuntimeVersionModulePacketSink
}
