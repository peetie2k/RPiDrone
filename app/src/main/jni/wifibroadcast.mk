LDFLAGS=-lpcap
CPPFLAGS=-Wall


gcc rx.o lib.o radiotap.o fec.o -o -lpcap