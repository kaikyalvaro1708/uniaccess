export function isValidCpf(value: string): boolean {
  const digits = value.replace(/\D/g, '')
  if (digits.length !== 11) return false
  if (/^(\d)\1+$/.test(digits)) return false

  const calc = (len: number) => {
    let sum = 0
    for (let i = 0; i < len; i++) sum += parseInt(digits[i]) * (len + 1 - i)
    const rem = 11 - (sum % 11)
    return rem >= 10 ? 0 : rem
  }

  return calc(9) === parseInt(digits[9]) && calc(10) === parseInt(digits[10])
}

export function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
}

// only plain ASCII letters and spaces, at least two words
export function isValidName(value: string): boolean {
  return /^[a-zA-Z]+([ ]+[a-zA-Z]+)+$/.test(value.trim())
}

export function isNotFutureDate(value: string): boolean {
  if (!value) return false
  return new Date(value) <= new Date()
}
