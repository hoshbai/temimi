import { describe, it, expect } from 'vitest'
import { toCamelCase, toSnakeCase } from './fieldMapper'

describe('fieldMapper', () => {
  describe('toCamelCase', () => {
    it('should convert snake_case keys to camelCase', () => {
      const input = {
        user_name: 'test',
        user_id: 123,
        is_active: true
      }
      const expected = {
        userName: 'test',
        userId: 123,
        isActive: true
      }
      expect(toCamelCase(input)).toEqual(expected)
    })

    it('should handle nested objects', () => {
      const input = {
        user_info: {
          first_name: 'John',
          last_name: 'Doe'
        }
      }
      const expected = {
        userInfo: {
          firstName: 'John',
          lastName: 'Doe'
        }
      }
      expect(toCamelCase(input)).toEqual(expected)
    })

    it('should handle arrays of objects', () => {
      const input = [
        { user_id: 1, user_name: 'Alice' },
        { user_id: 2, user_name: 'Bob' }
      ]
      const expected = [
        { userId: 1, userName: 'Alice' },
        { userId: 2, userName: 'Bob' }
      ]
      expect(toCamelCase(input)).toEqual(expected)
    })

    it('should handle primitive values', () => {
      expect(toCamelCase('test')).toBe('test')
      expect(toCamelCase(123)).toBe(123)
      expect(toCamelCase(true)).toBe(true)
      expect(toCamelCase(null)).toBe(null)
    })

    it('should preserve Date objects', () => {
      const date = new Date()
      expect(toCamelCase(date)).toBe(date)
    })
  })

  describe('toSnakeCase', () => {
    it('should convert camelCase keys to snake_case', () => {
      const input = {
        userName: 'test',
        userId: 123,
        isActive: true
      }
      const expected = {
        user_name: 'test',
        user_id: 123,
        is_active: true
      }
      expect(toSnakeCase(input)).toEqual(expected)
    })

    it('should handle nested objects', () => {
      const input = {
        userInfo: {
          firstName: 'John',
          lastName: 'Doe'
        }
      }
      const expected = {
        user_info: {
          first_name: 'John',
          last_name: 'Doe'
        }
      }
      expect(toSnakeCase(input)).toEqual(expected)
    })

    it('should handle arrays of objects', () => {
      const input = [
        { userId: 1, userName: 'Alice' },
        { userId: 2, userName: 'Bob' }
      ]
      const expected = [
        { user_id: 1, user_name: 'Alice' },
        { user_id: 2, user_name: 'Bob' }
      ]
      expect(toSnakeCase(input)).toEqual(expected)
    })

    it('should handle primitive values', () => {
      expect(toSnakeCase('test')).toBe('test')
      expect(toSnakeCase(123)).toBe(123)
      expect(toSnakeCase(true)).toBe(true)
      expect(toSnakeCase(null)).toBe(null)
    })

    it('should preserve Date objects', () => {
      const date = new Date()
      expect(toSnakeCase(date)).toBe(date)
    })
  })
})
