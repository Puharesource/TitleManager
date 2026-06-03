package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_15_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_15_R1", 9) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_15_R1.LegacyV1_15_R1RuntimeVersionModulePacketSink
}
