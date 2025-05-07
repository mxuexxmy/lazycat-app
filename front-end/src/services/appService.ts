import { lzcAPIGateway } from "@lazycatcloud/sdk";

// 初始化 lzcapi
const lzcapi = new lzcAPIGateway(window.location.origin, false);

export interface AppInfo {
  appid: string;
  status: number;
  instanceStatus: number;
  version?: string;
  title?: string;
  description?: string;
  icon?: string;
  domain?: string;
  downloadProgress?: {
    current: number;
    total: number;
  };
  builtin?: boolean;
  errorReason?: string;
  unsupportedPlatforms: string[];
  multiInstance: boolean;
}

export interface AppListResponse {
  infoList: AppInfo[];
}

export const getAppList = async (): Promise<AppInfo[]> => {
  try {
    // 使用 lzcapi 调用 package manager 服务以获取所有应用列表
    const response = await lzcapi.pkgm.QueryApplication({ appidList: [] });
    console.debug("applications: ", response);
    return response.infoList || [];
  } catch (error) {
    console.error("Error fetching app list:", error);
    // 如果连接失败，返回空列表
    return [];
  }
}; 