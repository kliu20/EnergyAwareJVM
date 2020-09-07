#include <stdio.h>
#include <stdint.h>

int main() {

	uint64_t mask = 1;
	uint64_t a = 111;
	int64_t b = -1;
	a <<= 61;
	mask <<= 63;
	printf("mask is: %lu\n", mask);
	//printf("a is: %lu\n", (~a + 1));
	printf("a is: %ld\n", a);
	a ^= mask;	
	printf("after xor a is: %ld\n", a);
}
