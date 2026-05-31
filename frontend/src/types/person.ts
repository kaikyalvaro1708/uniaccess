export interface RegisterPersonData {
  fullName: string
  document: string
  email: string
  dateOfBirth: string
  zipCode: string
  street: string
  neighborhood: string
  city: string
  state: string
  complement: string
}

export interface PersonDetailsData {
  id: number
  fullName: string
  document: string
  email: string
  dateOfBirth: string
  login: string
  createdAt: string
}

export interface ZipCodeDetails {
  zipCode: string
  street: string
  neighborhood: string
  city: string
  state: string
}

export interface ApiError {
  title: string
  status: number
  errors?: string[]
  detail?: string
}
