package com.nft.quizgame.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nft.quizgame.common.R;
import com.nft.quizgame.common.pref.PrefConst;
import com.nft.quizgame.common.pref.PrivatePreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用相关的工具类
 * @author yangguanxiang
 *
 */
public class AppUtils {
	private static final int NEW_MARKET_VERSION_CODE = 8006027;

    /**
	 * 检查是安装某包
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static boolean isAppExist(final Context context, final String packageName) {
		if (context == null || packageName == null) {
			return false;
		}

		boolean result = false;
		try {
			// context.createPackageContext(packageName,
			// Context.CONTEXT_IGNORE_SECURITY);
			context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_SHARED_LIBRARY_FILES);
			result = true;
		} catch (NameNotFoundException e) {
			result = false;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}
	/**
	 * 检查是安装某包
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static boolean isAppEnable(final Context context, final String packageName) {
		if (context == null || packageName == null) {
			return false;
		}
		
		boolean result = false;
		try {
			// context.createPackageContext(packageName,
			// Context.CONTEXT_IGNORE_SECURITY);
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName,
					PackageManager.GET_SHARED_LIBRARY_FILES);
			if (packageInfo != null && packageInfo.applicationInfo != null) {
				result = packageInfo.applicationInfo.enabled;
			}
		} catch (NameNotFoundException e) {
			result = false;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public static boolean isAppExist(final Context context, final Intent intent) {
		List<ResolveInfo> infos = null;
		try {
			infos = context.getPackageManager().queryIntentActivities(intent, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (infos != null) && (infos.size() > 0);
	}

	/**
	 * 获取app包信息
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @return
	 */
	public static PackageInfo getAppPackageInfo(final Context context, final String packageName) {
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(packageName, 0);
		} catch (Exception e) {
			info = null;
			e.printStackTrace();
		}
		return info;
	}

	public static long getAppFirstInstallTime(final Context context,
			final String packageName) {
		PackageInfo info = getAppPackageInfo(context, packageName);
		return getAppFirstInstallTime(context, info);
	}

	public static long getAppFirstInstallTime(final Context context,
			PackageInfo info) {
		if (null != info) {
			return info.firstInstallTime;
		}
		return 0;
	}

	/**
	 * 获取文件属性
	 * @param fileName
	 * @return
	 */
	public static String getFileOption(final String fileName) {
		String command = "ls -l " + fileName;
		StringBuffer sbResult = new StringBuffer();
		try {
			java.lang.Process proc = Runtime.getRuntime().exec(command);
			InputStream input = proc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String tmpStr = null;
			while ((tmpStr = br.readLine()) != null) {
				sbResult.append(tmpStr);
			}
			if (input != null) {
				input.close();
			}
			if (br != null) {
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sbResult.toString();
	}

	/**
	 * 服务是否正在运行
	 * 
	 * @param context
	 * @param packageName
	 *            包名
	 * @param serviceName
	 *            服务名
	 * @return
	 */
	public static boolean isServiceRunning(Context context, String packageName, String serviceName) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		return isServiceRunning(activityManager, packageName, serviceName);
	}

	public static boolean isServiceRunning(ActivityManager activityManager, String packageName,
                                           String serviceName) {
		List<RunningServiceInfo> serviceTasks = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		int sz = null == serviceTasks ? 0 : serviceTasks.size();
		for (int i = 0; i < sz; i++) {
			RunningServiceInfo info = serviceTasks.get(i);
			if (null != info && null != info.service) {
				final String pkgName = info.service.getPackageName();
				final String className = info.service.getClassName();

				if (pkgName != null && pkgName.contains(packageName) && className != null
						&& className.contains(serviceName)) {
					Logcat.i("Notification", "package = " + info.service.getPackageName()
							+ " class = " + info.service.getClassName());
					return true;
				}
			}
		}
		return false;
	}



	/**
	 * 取消指定的ID的Notificaiton
	 * @param context
	 * @param notificationId
	 */
	public static void cancelNotificaiton(Context context, int notificationId) {
		if (context != null) {
			try {
				NotificationManager nm = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(notificationId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 浏览器直接访问uri
	 * 
	 * @param uriString
	 * @return 成功打开返回true
	 */
	public static boolean gotoBrowser(Context context, String uriString) {
		boolean ret = false;
		if (uriString == null) {
			return ret;
		}
		Uri browserUri = Uri.parse(uriString);
		if (null != browserUri) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(browserIntent);
				ret = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * 卸载程序
	 * 
	 * @param context
	 *            上下文
	 * @param packageURI
	 *            需要卸载的程序的Uri
	 */
	public static void uninstallApp(Context context, Uri packageURI) {
		try {
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
			uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(uninstallIntent);
			uninstallIntent = null;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Uninstall failed", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 卸载包
	 * 
	 * @param context
	 *            上下文
	 * @param pkgName
	 *            需要卸载的程序的Uri
	 */
	public static void uninstallPackage(Context context, String pkgName) {
		Uri packageURI = Uri.parse("package:" + pkgName);
		uninstallApp(context, packageURI);
	}

	/**
	 * 是否激活设备
	 * @param pkg
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	private static boolean isAdminActive(String pkg, Context context) {
		boolean isActive = false;
		Intent intent = new Intent("android.app.action.DEVICE_ADMIN_ENABLED");
		PackageManager packageManager = context.getPackageManager();
		intent.setPackage(pkg);
		List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, 0);
		if (list != null && list.size() != 0) {
			if (Build.VERSION.SDK_INT > 7) {
				DevicePolicyManager devicepolicymanager = (DevicePolicyManager) context
						.getSystemService(Context.DEVICE_POLICY_SERVICE);
				isActive = devicepolicymanager.isAdminActive(new ComponentName(pkg,
						list.get(0).activityInfo.name));
			}
		}
		return isActive;
	}

	public static int getPidByProcessName(Context context, String processName) {
		int pid = 0;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
		if (appProcessList != null) {
			for (RunningAppProcessInfo runningAppProcessInfo : appProcessList) {
				if (runningAppProcessInfo.processName.equals(processName)) {
					pid = runningAppProcessInfo.pid;
					break;
				}
			}
		}
		return pid;
	}

	public static List<Integer> getPidsByProcessNamePrefix(Context context, String processNamePrefix) {
		if (TextUtils.isEmpty(processNamePrefix)) {
			return null;
		}
		List<Integer> pids = null;
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcessList = am.getRunningAppProcesses();
		if (appProcessList != null) {
			for (RunningAppProcessInfo runningAppProcessInfo : appProcessList) {
				if (runningAppProcessInfo.processName.startsWith(processNamePrefix)) {
					if (pids == null) {
						pids = new ArrayList<Integer>();
					}
					pids.add(runningAppProcessInfo.pid);
				}
			}
		}
		return pids;

	}

	/**
	 * 杀死当前进程
	 */
	public static void killProcess() {
		Logcat.i("Test",
				Log.getStackTraceString(new RuntimeException("kill process: " + Process.myPid())));
		killProcess(Process.myPid());
	}

	/**
	 * 杀死进程
	 */
	public static void killProcess(int pid) {
		Logcat.i("Test", Log.getStackTraceString(new RuntimeException("kill process: " + pid)));
		Process.killProcess(pid);
	}

	/**
	 * 跳到电子市场的我的应用界面
	 * @param context
	 * @return
	 */
	public static boolean gotoMarketMyApp(Context context) {
		boolean result = false;
		if (context == null) {
			return result;
		}
		String marketPkgName = "com.android.vending";
		int versionCode = getVersionCodeByPkgName(context, marketPkgName);
		Intent emarketIntent = null;
		if (versionCode >= NEW_MARKET_VERSION_CODE) {
			// 直接跳到电子市场我的应用界面
			emarketIntent = new Intent("com.google.android.finsky.VIEW_MY_DOWNLOADS");
			emarketIntent.setClassName(marketPkgName,
					"com.google.android.finsky.activities.MainActivity");
		} else {
			//跳转至电子市场首界面
			PackageManager packageMgr = context.getPackageManager();
			emarketIntent = packageMgr.getLaunchIntentForPackage(marketPkgName);
		}
		try {
			emarketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(emarketIntent);
			result = true;
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取指定包的版本号
	 *
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	public static int getVersionCodeByPkgName(Context context, String pkgName) {
		int versionCode = 1;
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionCode = pkgInfo.versionCode;
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		return versionCode;
	}

	/**
	 * 获取指定包的版本名称
	 *
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	public static String getVersionNameByPkgName(Context context, String pkgName) {
		String versionName = "1.0";
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionName = pkgInfo.versionName;
			} catch (Exception e) {
				//NOT to do anything
				//e.printStackTrace();
			}
		}
		return versionName;
	}

	/**
	 * 将版本名称转换为一位小数点的float型数据
	 *
	 * @param versionName
	 * @param versionName
	 */
	public static float changeVersionNameToFloat(String versionName) {
		float versionNumber = 0.0f;
		if (versionName != null && !versionName.equals("")) {
			try {
				versionName = versionName.trim().toLowerCase();
				String underLine = "_";
				if (versionName.contains(underLine)) {
					versionName = versionName.substring(0, versionName.indexOf(underLine));
				}
				String beta = "beta";
				if (versionName.contains(beta)) {
					versionName = versionName.replace(beta, "");
				}
				int firstPoint = versionName.indexOf(".");
				int secondPoint = versionName.indexOf(".", firstPoint + 1);
				if (secondPoint != -1) {
					String temp = versionName.substring(0, secondPoint)
							+ versionName.substring(secondPoint + 1);
					versionNumber = Float.parseFloat(temp);
				} else {
					versionNumber = Float.parseFloat(versionName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return versionNumber;
	}


	/**
	 * Check if the installed Gmail app supports querying for label information.
	 *
	 * @param c
	 *            an application Context
	 * @return true if it's safe to make label API queries
	 */
	public static boolean canReadGmailLabels(Context c) {
		/**
		 * Permission required to access this
		 * {@link android.content.ContentProvider}
		 */
		final String permission = "com.google.android.gm.permission.READ_CONTENT_PROVIDER";
		/**
		 * Authority for the Gmail content provider.
		 */
		final String authority = "com.google.android.gm";
		String gmailPackageName = "com.google.android.gm";

		boolean supported = false;

		try {
			final PackageInfo info = c.getPackageManager().getPackageInfo(gmailPackageName,
					PackageManager.GET_PROVIDERS | PackageManager.GET_PERMISSIONS);
			boolean allowRead = false;
			if (info.permissions != null) {
				for (int i = 0, len = info.permissions.length; i < len; i++) {
					final PermissionInfo perm = info.permissions[i];
					if (permission.equals(perm.name)
							&& perm.protectionLevel < PermissionInfo.PROTECTION_SIGNATURE) {
						allowRead = true;
						break;
					}
				}
			}
			if (allowRead && info.providers != null) {
				for (int i = 0, len = info.providers.length; i < len; i++) {
					final ProviderInfo provider = info.providers[i];
					if (authority.equals(provider.authority)
							&& TextUtils.equals(permission, provider.readPermission)) {
						supported = true;
					}
				}
			}
		} catch (NameNotFoundException e) {
			// Gmail app not found
		}
		return supported;
	}

	public static String getCurProcessName(Context context) {
		int myPid = Process.myPid();
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcessList = activityManager.getRunningAppProcesses();
		if (appProcessList != null) {
			for (RunningAppProcessInfo appProcess : appProcessList) {
				if (appProcess.pid == myPid) {
					return appProcess.processName;
				}
			}
		}

		java.lang.Process process = null;
		BufferedReader reader = null;
		try {
			process = Runtime.getRuntime().exec("ps " + myPid);
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = reader.readLine();
			String[] texts = null;
			if (line != null) { //第一行为标题
				Logcat.i("Test", "line: " + line);
				if ((line = reader.readLine()) != null) { //第二行才是数据
					Logcat.i("Test", "line: " + line);
					texts = line.split("\\s+", Integer.MAX_VALUE);
					String name = texts[texts.length - 1];
					return name;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (process != null) {
				process.destroy();
			}
		}
		return null;
	}


	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 *
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 *
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取设备基本信息
	 * @return
	 */
	public static String getBaseDeviceInfo() {
		StringBuilder baseInfo = new StringBuilder();
		try {
			String product = "Product=" + Build.PRODUCT;
			String phoneModel = "\nPhoneModel=" + Build.MODEL;
			String kernel = "\nKernel=" + Machine.getLinuxKernel();
			String rom = "\nROM=" + Build.DISPLAY;
			String board = "\nBoard=" + Build.BOARD;
			String device = "\nDevice=" + Build.DEVICE;
			//			String density = "\nDensity="
			//					+ String.valueOf(context.getResources().getDisplayMetrics().density);
			//			String packageName = "\nPackageName=" + context.getPackageName();
			String androidVersion = "\nAndroidVersion=" + Build.VERSION.RELEASE;
			String totalMemSize = "\nTotalMemSize=" + (getTotalInternalMemorySize() / 1024 / 1024)
					+ "MB";
			String freeMemSize = "\nFreeMemSize="
					+ (getAvailableInternalMemorySize() / 1024 / 1024) + "MB";
			String romAppHeapSize = "\nRom App Heap Size="
					+ (int) (Runtime.getRuntime().maxMemory() / 1024L / 1024L)
					+ "MB";
			baseInfo.append(product).append(phoneModel).append(kernel).append(rom).append(board)
					.append(device).append(androidVersion).append(totalMemSize).append(freeMemSize)
					.append(romAppHeapSize);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return baseInfo.toString();
	}

	/**
	 * 判断Activity是否显示在前台
	 * @param context
	 * @param packageName 应用包名
	 * @param activityName 应用Activity名称
	 * @return true:显示在前台  false:不是显示在前台
	 */
	public static boolean isTopActivity(Context context, String packageName, String activityName) {
		if (context == null || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(activityName)) {
			return false;
		}
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        return componentName != null && packageName.equals(componentName.getPackageName())
                && activityName.equals(componentName.getClassName());
    }

	public static long getPackageSize(Context context, String packageName) {
		try {
			ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
			if (applicationInfo != null && applicationInfo.sourceDir != null) {
				return new File(applicationInfo.sourceDir).length();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public static boolean isInstallOnSDCard(Context context , String packageName) {
		ApplicationInfo appInfo;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);

			if ((appInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public static long getInstalledTime(Context context, String packageName) {
		String sourceDir = null;
		try {
			sourceDir = context.getPackageManager().getApplicationInfo(packageName, 0).sourceDir;
			File file = new File(sourceDir);
			return file.lastModified();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0L;
	}
	
	/**
	 * <br>功能简述:用webview打开
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param mContext
	 * @param url
	 * @param clazz 
	 */
	public static void gotoWebView(Context mContext, String url, Class<?> clazz) {
		Intent intent;
		if (clazz != null) {
			intent = new Intent(mContext, clazz);
		} else {
			intent = new Intent(Intent.ACTION_VIEW);
		}
		intent.putExtra("url", url);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}
	
	private static boolean isTopActivity(Context context, String packageName) {
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
            if (tasksInfo.size() > 0) {
                // Activity位于堆栈的顶层,如果Activity的类为空则判断的是当前应用是否在前台
                if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
    
    public static boolean isForegroundApp(Context context, String pkgName) {
        // 获取当前正在运行进程列表
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false; 
            }
            
            for (RunningAppProcessInfo appProcess : appProcesses) {
                // 通过进程名及进程所用到的包名来进行查找
                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    if (appProcess.processName.equals(pkgName)
                            || Arrays.asList(appProcess.pkgList).contains(pkgName)) {
                        return true; 
                    }
                } 
            } 
        } catch (Exception e) {
        }
        return false; 
    }

	public static boolean isForegroundTopApp(Context context, String pkgName) {
		// 获取当前正在运行进程列表
		try {
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
			if (appProcesses == null) {
				return false;
			}
			RunningAppProcessInfo runningAppProcessInfo = appProcesses.get(0);
// 通过进程名及进程所用到的包名来进行查找
			if (runningAppProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				if (runningAppProcessInfo.processName.equals(pkgName)) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
     * 判断当前程序是否运行于前台 
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isAppRunningInForeground(Context context, String pkgName) {
        if (Machine.IS_SDK_ABOVE_LOLIP) {
            return isForegroundApp(context, pkgName);
        } else {
            return isTopActivity(context, pkgName);
        }
    }
    
	public static String getTopActivity(Context context) {
		String packageName = null;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		if (Machine.IS_SDK_ABOVE_LOLIP) {
			List<RunningAppProcessInfo> runningProcessInfos = activityManager
					.getRunningAppProcesses();
			if (runningProcessInfos != null && !runningProcessInfos.isEmpty()) {
				for (RunningAppProcessInfo runningAppProcessInfo : runningProcessInfos) {
					if (runningAppProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
							&& runningAppProcessInfo.pkgList != null
							&& runningAppProcessInfo.pkgList.length > 0) {
						packageName = runningAppProcessInfo.pkgList[0];
						break;
					}
				}
			}
		} else {
			ComponentName componentName = null;
			componentName = activityManager.getRunningTasks(1).get(0).topActivity;
			if (componentName != null) {
				packageName = componentName.getPackageName();
			}
		}

		return packageName;
	}

	public static void triggerAlarm(AlarmManager alarmManager, int type, long triggerAtMillis,
									PendingIntent operation) {
		try {
			if (Machine.IS_SDK_ABOVE_KITKAT) {
				alarmManager.setExact(type, triggerAtMillis, operation);
			} else {
				alarmManager.set(type, triggerAtMillis, operation);
			}
		} catch (Exception e) {

		}
	}

	public static int getSvnCode(Context context) {
		return getMetaData(context, "svn", 0);
	}

	public static String getChannel(Context context) {
        return String.valueOf(getMetaData(context, "channel", 230));
	}

	public static String getStore(Context context) {
        return getMetaData(context, "store", "");
	}

    /**
     * 是否商店包
     * @param context
     * @return
     */
	public static boolean isStorePkg(Context context){
		return !TextUtils.isEmpty(AppUtils.getStore(context));
	}

	public static String getBuglyAppId(Context context) {
		return getMetaData(context, "bugly_app_id", "");
	}

	public static String getBuglyAppKey(Context context) {
		return getMetaData(context, "bugly_app_key", "");
	}

	public static String getBuglyChannel(Context context) {
		return getMetaData(context, "bugly_channel", "");
	}

    public static String getMetaData(Context context, String key, String defaultValue) {
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData;
            return metaData.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static int getMetaData(Context context, String key, int defaultValue) {
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData;
            return metaData.getInt(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

	public static long getInstallTime(Context context) {
		return PrivatePreference.Companion.getPreference().getValue(PrefConst.KEY_FIRST_RUN_TIME, AppUtils.getInstalledTime(context, context.getPackageName()));
	}

	public static int getCdays(Context context) {
		int cdays = 1;
		PrivatePreference pref = PrivatePreference.Companion.getPreference();
		long firstRunTime = pref.getValue(PrefConst.KEY_FIRST_RUN_TIME, 0L);
		if (firstRunTime > 0) {
			long diff = System.currentTimeMillis() - firstRunTime;
			cdays = Math.round(diff / 1000 / 86400);
			if (cdays < 1) {
				cdays = 1;
			} else {
				cdays += 1;
			}
		}
		return cdays;
	}

	/**
	 * 跳转到浏览器
	 */
	public static boolean openBrowser(Context context, String urlString) {
		return !TextUtils.isEmpty(urlString) && openActivitySafely(context, Intent.ACTION_VIEW, urlString, null);
	}

	public static boolean openActivitySafely(Context context, String action, String uri, String packageName) {
		boolean isOk = true;
		try {
			Uri uriData = Uri.parse(uri);
			final Intent intent = new Intent(action, uriData);
			if (!TextUtils.isEmpty(packageName)) {
				intent.setPackage(packageName);
			}
			if (!(context instanceof Activity)) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			context.startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
			isOk = false;
		}
		return isOk;
	}

	/**
	 * 修复Android 8.0 Only fullscreen activities can request orientation
	 *
	 * @param context
	 * @return
	 */
	public static boolean isTranslucentOrFloating(Context context) {
		boolean isTranslucentOrFloating = false;
		try {
			int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
			final TypedArray ta = context.obtainStyledAttributes(styleableRes);
			Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
			m.setAccessible(true);
			isTranslucentOrFloating = (boolean) m.invoke(null, ta);
			m.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isTranslucentOrFloating;
	}

	/**
	 * 修复Android 8.0 Only fullscreen activities can request orientation
	 * * @param context
	 * * @return
	 */
	public static boolean fixOrientation(Context context) {
		try {
			Field field = Activity.class.getDeclaredField("mActivityInfo");
			field.setAccessible(true);
			ActivityInfo o = (ActivityInfo) field.get(context);
			o.screenOrientation = -1;
			field.setAccessible(false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getABTestUser(Context context) {
		try {
			Bundle metaData = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA).metaData;
			return String.valueOf(metaData.get("abTestUser"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getGoogleAdvertisingId() {
		return "";
	}


	public static String convertCoin(Context context,int coin){
		String result;
		if (coin == 0) {
			result= context.getString(R.string.coin_convert_money_symbol, coin,"0");
		} else {

			String format = String.format(Locale.CHINA, "%.2f",coin * 1f / 10000);
			result =  context.getString(R.string.coin_convert_money_symbol, coin,format);
		}
		return result;
	}

	public static boolean isEmail(String string) {
		if (string == null)
			return false;
		String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern p;
		Matcher m;
		p = Pattern.compile(regEx1);
		m = p.matcher(string);
		return m.matches();
	}

}
