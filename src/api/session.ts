import type { AuthResponse } from '../types/api'

const STORAGE_KEY = 'barniz-express.session'

type Session = { token: string; expiresAt: string }

let session: Session | null = null
const listeners = new Set<() => void>()

function readStoredSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<Session> | null
    if (!parsed || typeof parsed.token !== 'string' || typeof parsed.expiresAt !== 'string') {
      sessionStorage.removeItem(STORAGE_KEY)
      return null
    }
    if (Date.parse(parsed.expiresAt) < Date.now()) {
      sessionStorage.removeItem(STORAGE_KEY)
      return null
    }
    return { token: parsed.token, expiresAt: parsed.expiresAt }
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

/** Re-reads the persisted session (memory first, then sessionStorage). */
export function hydrateSession(): string | null {
  if (session) return session.token
  session = readStoredSession()
  return session?.token ?? null
}

export function getToken(): string | null {
  return hydrateSession()
}

export function setSession(response: AuthResponse): void {
  session = { token: response.token, expiresAt: response.expiresAt }
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch {
    // Storage may be unavailable (private mode); memory token still works.
  }
}

export function clearSession(): void {
  session = null
  try {
    sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // Ignore storage failures.
  }
}

/** Fires when the API answers 401 so the app can bounce back to login. */
export function notifyUnauthorized(): void {
  clearSession()
  listeners.forEach((listener) => listener())
}

export function subscribeToUnauthorized(listener: () => void): () => void {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}
