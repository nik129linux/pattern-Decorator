import type { Product, ShippingOption } from '../types/api'
import { request } from './client'

export function getProducts(): Promise<Product[]> {
  return request<Product[]>('/api/v1/products')
}

export function getOptions(): Promise<ShippingOption[]> {
  return request<ShippingOption[]>('/api/v1/options')
}
