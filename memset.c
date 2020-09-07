#include <stdio.h>


int main() {
	char a[5][5];
	//memset(a,'\0',sizeof(a));
	FILE* omg;
	a[0][0]='k';
	printf("%s",a[0]);
	printf("Size is %d \n", sizeof(a));
	return 0;
}
