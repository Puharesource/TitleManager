package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_9_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_9_R1", 3) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_9_R1.LegacyV1_9_R1RuntimeVersionModulePacketSink
}
