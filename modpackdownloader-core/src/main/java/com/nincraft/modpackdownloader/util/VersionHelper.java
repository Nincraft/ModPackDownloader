package com.nincraft.modpackdownloader.util;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class VersionHelper {
	public static int compareVersions(String manifestForgeVersion, String updatedForgeVersion) {
		val manArr = manifestForgeVersion.split("\\.");
		val updateArr = updatedForgeVersion.split("\\.");
		int i = 0;
		while (i < manArr.length || i < updateArr.length) {
			if (Integer.parseInt(manArr[i]) < Integer.parseInt(updateArr[i])) {
				return -1;
			} else if (Integer.parseInt(manArr[i]) > Integer.parseInt(updateArr[i])) {
				return 1;
			}
			i++;
		}
		return 0;
	}
}
