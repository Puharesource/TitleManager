package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_9_R2RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_9_R2", 3) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_9_R2.LegacyV1_9_R2RuntimeVersionModulePacketSink
}
