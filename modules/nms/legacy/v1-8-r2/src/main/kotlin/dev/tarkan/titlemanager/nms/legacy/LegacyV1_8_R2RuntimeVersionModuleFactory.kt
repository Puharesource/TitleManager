package dev.tarkan.titlemanager.nms.legacy

class LegacyV1_8_R2RuntimeVersionModuleFactory : LegacyDirectNmsRuntimeVersionModuleFactory("v1_8_R2", 2) {
    override fun createPacketSink(): LegacyDirectNmsPacketSink = dev.tarkan.titlemanager.nms.legacy.v1_8_R2.LegacyV1_8_R2RuntimeVersionModulePacketSink
}
