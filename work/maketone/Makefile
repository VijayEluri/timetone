TARGET = mktone
CC     = gcc
OBJS   = mktone.c
CFLAGS = 
LIBS   = 

all : $(TARGET)
	$(TARGET)

$(TARGET) : $(OBJS)
	$(CC) -o $@ $(OBJS) $(LIBS)

.c.o :
	$(CC) $(CFLAGS) -c $<
