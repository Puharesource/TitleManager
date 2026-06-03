package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_13_R2RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_13_R2", 8) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_13_R2.LegacyV1_13_R2RuntimeVersionModulePacketSink
}
