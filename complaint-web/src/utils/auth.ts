const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const USER_INFO_KEY = 'user_info'

export interface UserInfo {
  userId: number
  username: string
  realName: string
  deptName: string
  roles: string[]
  permissions: string[]
  avatar?: string
  phone?: string
  email?: string
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function getUserInfo(): UserInfo | null {
  const info = localStorage.getItem(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setUserInfo(info: UserInfo): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(info))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

export function clearAll(): void {
  removeToken()
  removeUserInfo()
}
