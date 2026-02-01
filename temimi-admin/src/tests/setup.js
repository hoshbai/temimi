// Test setup file
import { config } from '@vue/test-utils'

// Mock localStorage
global.localStorage = {
  store: {},
  getItem(key) {
    return this.store[key] || null
  },
  setItem(key, value) {
    this.store[key] = value.toString()
  },
  removeItem(key) {
    delete this.store[key]
  },
  clear() {
    this.store = {}
  }
}

// Mock Element Plus message
config.global.mocks = {
  $message: {
    success: () => {},
    error: () => {},
    warning: () => {},
    info: () => {}
  }
}
