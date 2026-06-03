package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_13_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_13_R1", 7) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_13_R1.LegacyV1_13_R1RuntimeVersionModulePacketSink
}
