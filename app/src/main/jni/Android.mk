LOCAL_PATH := $(call my-dir)
MY_PATH := $(LOCAL_PATH)


####################
# Build GSTREAMER
#
# Edit this line
GSTREAMER_ROOT_ANDROID := C:\\AndroidDev\\gstreamer_1_4_5
SHELL := PATH=/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin /bin/bash

include $(CLEAR_VARS)

LOCAL_MODULE    := videoPlayback
LOCAL_SRC_FILES := videoPlayback.cpp
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -llog -landroid
LOCAL_CFLAGS := -fpermissive
include $(BUILD_SHARED_LIBRARY)



ifndef GSTREAMER_ROOT
ifndef GSTREAMER_ROOT_ANDROID
$(error GSTREAMER_ROOT_ANDROID is not defined!)
endif
GSTREAMER_ROOT        := $(GSTREAMER_ROOT_ANDROID)
endif
GSTREAMER_NDK_BUILD_PATH  := $(GSTREAMER_ROOT)/share/gst-android/ndk-build


include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk
GSTREAMER_PLUGINS         := udp tcp gdp rtp libav autodetect videoconvert videoparsersbad $(GSTREAMER_PLUGINS_CORE) $(GSTREAMER_PLUGINS_PLAYBACK) $(GSTREAMER_PLUGINS_CODECS) $(GSTREAMER_PLUGINS_NET) $(GSTREAMER_PLUGINS_SYS) $(GSTREAMER_PLUGINS_CODECS_RESTRICTED)
G_IO_MODULES              := gnutls
GSTREAMER_EXTRA_DEPS      := gstreamer-video-1.0

include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk



########################
# Build Libpcap
#
include $(CLEAR_VARS)
LOCAL_MODULE:= libpcap
LOCAL_SRC_FILES:= \
	libpcap-1.7.4/pcap-linux.c \
	libpcap-1.7.4/fad-null.c \
	libpcap-1.7.4/pcap.c \
	libpcap-1.7.4/inet.c \
	libpcap-1.7.4/gencode.c \
	libpcap-1.7.4/optimize.c \
	libpcap-1.7.4/nametoaddr.c \
	libpcap-1.7.4/etherent.c \
	libpcap-1.7.4/savefile.c \
	libpcap-1.7.4/sf-pcap.c \
	libpcap-1.7.4/sf-pcap-ng.c \
	libpcap-1.7.4/pcap-common.c \
	libpcap-1.7.4/bpf_image.c \
	libpcap-1.7.4/bpf_dump.c \
	libpcap-1.7.4/scanner.c \
	libpcap-1.7.4/grammar.c \
	libpcap-1.7.4/bpf_filter.c \
	libpcap-1.7.4/version.c \
	pcap_jni.c
#include $(BUILD_EXECUTABLE)
#LOCAL_CFLAGS	:= -DSYS_ANDROID=1 -Dyylval=pcap_lval -DHAVE_CONFIG_H  -D_U_="__attribute__((unused))"
LOCAL_C_INCLUDES :=\
	$(LOCAL_PATH)/libpcap-1.7.4
LOCAL_CFLAGS	:= -DSYS_ANDROID=1 -DHAVE_CONFIG_H -D_U_="__attribute__((unused))" -I$(LOCAL_PATH)/libpcap-1.7.4
LOCAL_LDLIBS	:= -llog -landroid
include $(BUILD_SHARED_LIBRARY)






#IW
include $(MY_PATH)/iw/libnl/Android.mk
include $(MY_PATH)/iw/Android.mk
include $(MY_PATH)/wtools/Android.mk



############################
# IW
############################

#include $(LOCAL_PATH)/iw/Android.mk
#include $(BUILD_SHARED_LIBRARY)


#IW_SOURCE_DIR := $(LOCAL_PATH)/iw
#
#include $(CLEAR_VARS)
#
#IW_ANDROID_BUILD=y
#NO_PKG_CONFIG=y
#include $(IW_SOURCE_DIR)/Makefile
#
#LOCAL_SRC_FILES := $(patsubst $(IW_SOURCE_DIR)/%.o,$(IW_SOURCE_DIR)/%.c,$(OBJS))
#
#LOCAL_CFLAGS += -DCONFIG_LIBNL20
#LOCAL_LDFLAGS := -Wl,--no-gc-sections
#LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_TAGS := eng
#LOCAL_STATIC_LIBRARIES := libnl
#LOCAL_MODULE := iw
#
#$(IW_SOURCE_DIR)/version.c:
#	$(IW_SOURCE_DIR)/version.sh $(IW_SOURCE_DIR)/version.c
#
#include $(BUILD_EXECUTABLE)
#include $(BUILD_SHARED_LIBRARY)



#Wifibroadcast
#LOCAL_MODULE    := wb_receiver
#LOCAL_SRC_FILES := rx.c
#LOCAL_SHARED_LIBRARIES := libpcap
#LOCAL_LDLIBS := -llog -landroid -lrt -lpcap
#LOCAL_CFLAGS := -fpermissive
#include $(BUILD_SHARED_LIBRARY)


