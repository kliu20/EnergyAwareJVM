#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "msr.h"

int *fd;
char *ener_info;
rapl_msr_unit rapl_unit;
rapl_msr_parameter *parameters;


int get_pos_intnum(int value) {
	int num = 1;
	while(value > 9) {
		num++;
		value /= 10;
	}
	
	return num;
}


int check_write_gov(char gov_file[60], const char *target) {
	int i;
	int rc;
	FILE *f;
    size_t data_length, data_written;
	char string[25];

	f = fopen(gov_file, "r");
	if (f == NULL) {
		//LOGI("Failed to open %s: %s", filename, strerror(errno));
		printf("Failed to open %s", gov_file);
		return 1;
	}

	fscanf(f, "%s", string);

	rc = fclose(f);
	if (rc != 0) {
		//LOGI("Failed to close %s: %s", filename, strerror(rc));
		printf("Failed to close %s", gov_file);
		return 1;
	}

	if (strcmp(string, target) != 0) {
		//Write govenor
		f = fopen(gov_file, "w");
		if (f == NULL) {
			//LOGI("Failed to open %s: %s", gov_file, strerror(errno));
			printf("Failed to open %s", gov_file);
			return 1;
		}

		data_length = strlen(target);
		data_written = fwrite(target, 1, data_length, f);
		if (data_length != data_written) {
			//LOGI("Failed to write to %s: %s", filename, strerror(errno));
			printf("Failed to write %s", target);
			return 1;
		}

		rc = fclose(f);
		if (rc != 0) {
			//LOGI("Failed to close %s: %s", filename, strerror(rc));
			printf("Failed to close %s", gov_file);
			return 1;
		}
	}
	return 0;

}

write_freq_coreId(char filename[60], int freq) {
	FILE *f;
        int rc;
        size_t data_length, data_written;
	int cpu_freq;
	int scal_cpufreq;

	f = fopen(filename, "w");
	if (f == NULL) {
		//LOGI("Failed to open %s: %s", filename, strerror(errno));
		printf("Failed to open %s\n", filename);
		return 1;
	}

//    data_length = strlen(freq);
 //   data_written = fwrite(freq, 1, data_length, f); //For binary code
	data_length = get_pos_intnum(freq);
	data_written = fprintf(f, "%d", freq);  //For integer

	if (data_length != data_written) {
		//LOGI("Failed to write to %s: %s", filename, strerror(errno));
		printf("Failed to write %s\n", filename);
		return 1;
	}

	rc = fclose(f);
	if (rc != 0) {
		//LOGI("Failed to close %s: %s", filename, strerror(rc));
		printf("Failed to close %s\n", filename);
		return 1;
	}
}
