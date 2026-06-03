import type { PersonDetailsData, RegisterPersonData, ZipCodeDetails } from '../types/person'

const BASE = '/api'

export async function fetchZipCode(zipCode: string): Promise<ZipCodeDetails> {
  const clean = zipCode.replace(/\D/g, '')
  const res = await fetch(`${BASE}/zip-code/${clean}`)
  if (!res.ok) throw await res.json()
  return res.json()
}

export async function registerPerson(data: RegisterPersonData): Promise<PersonDetailsData> {
  const res = await fetch(`${BASE}/persons`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ...data,
      document: data.document.replace(/\D/g, ''),
      zipCode: data.zipCode.replace(/\D/g, ''),
    }),
  })
  if (!res.ok) throw await res.json()
  return res.json()
}

export async function findPersonByLogin(login: string): Promise<PersonDetailsData> {
  const res = await fetch(`${BASE}/persons/login/${login.trim().toLowerCase()}`)
  if (!res.ok) throw await res.json()
  return res.json()
}

export async function findPersonByEmail(email: string): Promise<PersonDetailsData> {
  const res = await fetch(`${BASE}/persons/email/${encodeURIComponent(email.trim().toLowerCase())}`)
  if (!res.ok) throw await res.json()
  return res.json()
}

export async function fetchPersons(page = 0, size = 10) {
  const res = await fetch(`${BASE}/persons?page=${page}&size=${size}&sort=fullName,asc`)
  if (!res.ok) throw await res.json()
  return res.json()
}
