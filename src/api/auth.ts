import type { AuthResponse } from '../types/api'
import { request } from './client'

/** POST /api/v1/auth/login - the only route that does not need a token. */
export function login(username: string, password: string): Promise<AuthResponse> {
  return request<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: { username, password },
    auth: false,
  })
}
