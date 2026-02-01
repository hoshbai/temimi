/**
 * Field mapper utility for transforming data between frontend and backend formats
 * Frontend uses camelCase, backend uses snake_case
 */

/**
 * Convert snake_case to camelCase
 * @param {*} obj - Object, array, or primitive value to convert
 * @returns {*} Converted value with camelCase keys
 */
export function toCamelCase(obj) {
  if (Array.isArray(obj)) {
    return obj.map(item => toCamelCase(item))
  }
  
  if (obj !== null && typeof obj === 'object' && !(obj instanceof Date)) {
    return Object.keys(obj).reduce((result, key) => {
      // Convert snake_case to camelCase
      const camelKey = key.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
      result[camelKey] = toCamelCase(obj[key])
      return result
    }, {})
  }
  
  return obj
}

/**
 * Convert camelCase to snake_case
 * @param {*} obj - Object, array, or primitive value to convert
 * @returns {*} Converted value with snake_case keys
 */
export function toSnakeCase(obj) {
  if (Array.isArray(obj)) {
    return obj.map(item => toSnakeCase(item))
  }
  
  if (obj !== null && typeof obj === 'object' && !(obj instanceof Date)) {
    return Object.keys(obj).reduce((result, key) => {
      // Convert camelCase to snake_case
      const snakeKey = key.replace(/([A-Z])/g, '_$1').toLowerCase()
      result[snakeKey] = toSnakeCase(obj[key])
      return result
    }, {})
  }
  
  return obj
}
