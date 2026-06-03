package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_11_R1RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_11_R1", 5) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_11_R1.LegacyV1_11_R1RuntimeVersionModulePacketSink
}
