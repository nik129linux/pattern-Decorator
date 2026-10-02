import { setupWorker } from 'msw/browser'
import { handlers } from './handlers'

/** Serves the real REST contract from local fixtures when VITE_USE_MOCKS is on. */
export const worker = setupWorker(...handlers)
