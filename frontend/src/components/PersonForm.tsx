import React, { useState } from "react";
import FormField from "./FormField";
import { maskCpf, maskZipCode } from "../utils/masks";
import {
  isValidCpf,
  isValidEmail,
  isValidName,
  isNotFutureDate,
} from "../utils/validators";
import { fetchZipCode, registerPerson } from "../services/api";
import type { PersonDetailsData, RegisterPersonData } from "../types/person";

interface PersonFormProps {
  onSuccess: (person: PersonDetailsData) => void;
}

const EMPTY_FORM: RegisterPersonData = {
  fullName: "",
  document: "",
  email: "",
  dateOfBirth: "",
  zipCode: "",
  street: "",
  neighborhood: "",
  city: "",
  state: "",
  complement: "",
};

const PersonForm: React.FC<PersonFormProps> = ({ onSuccess }) => {
  const [form, setForm] = useState<RegisterPersonData>(EMPTY_FORM);
  const [errors, setErrors] = useState<Partial<RegisterPersonData>>({});
  const [apiError, setApiError] = useState("");
  const [loading, setLoading] = useState(false);
  const [zipLoading, setZipLoading] = useState(false);

  const handleChange = (field: keyof RegisterPersonData, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors((prev) => ({ ...prev, [field]: "" }));
    setApiError("");
  };

  const handleZipBlur = async () => {
    const clean = form.zipCode.replace(/\D/g, "");
    if (clean.length !== 8) return;

    setZipLoading(true);
    try {
      const addr = await fetchZipCode(clean);
      setForm((prev) => ({
        ...prev,
        street: addr.street ?? "",
        neighborhood: addr.neighborhood ?? "",
        city: addr.city ?? "",
        state: addr.state ?? "",
      }));
      setErrors((prev) => ({
        ...prev,
        zipCode: "",
        street: "",
        neighborhood: "",
        city: "",
        state: "",
      }));
    } catch {
      setErrors((prev) => ({ ...prev, zipCode: "CEP não encontrado" }));
    } finally {
      setZipLoading(false);
    }
  };

  const validate = (): boolean => {
    const e: Partial<RegisterPersonData> = {};

    if (!isValidName(form.fullName))
      e.fullName = "Mínimo duas palavras, apenas letras";
    if (!isValidCpf(form.document))
      e.document = "CPF inválido";
    if (!isValidEmail(form.email))
      e.email = "E-mail inválido";
    if (!form.dateOfBirth)
      e.dateOfBirth = "Data obrigatória";
    else if (!isNotFutureDate(form.dateOfBirth))
      e.dateOfBirth = "Data não pode ser futura";
    if (form.zipCode.replace(/\D/g, "").length !== 8)
      e.zipCode = "CEP inválido";
    if (!form.street.trim())       e.street       = "Obrigatório";
    if (!form.neighborhood.trim()) e.neighborhood = "Obrigatório";
    if (!form.city.trim())         e.city         = "Obrigatório";
    if (!form.state.trim())        e.state        = "Obrigatório";

    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    setApiError("");

    try {
      onSuccess(await registerPerson(form));
    } catch (err: unknown) {
      const apiErr = err as {
        status?: number;
        detail?: string;
        errors?: string[];
      };
      if (apiErr.status === 409)
        setErrors((prev) => ({ ...prev, document: "CPF já cadastrado" }));
      else if (apiErr.errors?.length)
        setApiError(apiErr.errors.join(" · "));
      else
        setApiError(apiErr.detail ?? "Erro inesperado. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">

      {/* informações pessoais */}
      <div className="flex flex-col gap-3">
        <SectionDivider>Informações pessoais</SectionDivider>

        <FormField
          label="Nome completo"
          name="fullName"
          value={form.fullName}
          onChange={(v) => handleChange("fullName", v)}
          error={errors.fullName}
          placeholder="Maria Silva Santos"
        />

        <div className="grid grid-cols-2 gap-3">
          <FormField
            label="CPF"
            name="document"
            value={form.document}
            onChange={(v) => handleChange("document", maskCpf(v))}
            error={errors.document}
            placeholder="000.000.000-00"
            maxLength={14}
          />
          <FormField
            label="Data de nascimento"
            name="dateOfBirth"
            type="date"
            value={form.dateOfBirth}
            onChange={(v) => handleChange("dateOfBirth", v)}
            error={errors.dateOfBirth}
            max={new Date().toISOString().split("T")[0]}
          />
        </div>

        <FormField
          label="E-mail"
          name="email"
          type="email"
          value={form.email}
          onChange={(v) => handleChange("email", v)}
          error={errors.email}
          placeholder="maria@email.com"
        />
      </div>

      {/* endereço */}
      <div className="flex flex-col gap-3">
        <SectionDivider>Endereço</SectionDivider>

        <div className="flex items-end gap-2">
          <div className="flex-1">
            <FormField
              label="CEP"
              name="zipCode"
              value={form.zipCode}
              onChange={(v) => handleChange("zipCode", maskZipCode(v))}
              onBlur={handleZipBlur}
              error={errors.zipCode}
              placeholder="00000-000"
              maxLength={9}
            />
          </div>
          {zipLoading && (
            <div className="h-10 flex items-center">
              <div className="w-4 h-4 border-2 border-orange border-t-transparent rounded-full animate-spin" />
            </div>
          )}
        </div>

        <FormField
          label="Logradouro"
          name="street"
          value={form.street}
          onChange={(v) => handleChange("street", v)}
          error={errors.street}
          placeholder="Avenida Paulista"
          disabled={zipLoading}
        />

        <div className="grid grid-cols-3 gap-3">
          <div className="col-span-1">
            <FormField
              label="Bairro"
              name="neighborhood"
              value={form.neighborhood}
              onChange={(v) => handleChange("neighborhood", v)}
              error={errors.neighborhood}
              disabled={zipLoading}
            />
          </div>
          <FormField
            label="Cidade"
            name="city"
            value={form.city}
            onChange={(v) => handleChange("city", v)}
            error={errors.city}
            disabled={zipLoading}
          />
          <FormField
            label="UF"
            name="state"
            value={form.state}
            onChange={(v) =>
              handleChange("state", v.toUpperCase().slice(0, 2))
            }
            error={errors.state}
            placeholder="SP"
            maxLength={2}
            disabled={zipLoading}
          />
        </div>

        <FormField
          label="Complemento"
          name="complement"
          value={form.complement}
          onChange={(v) => handleChange("complement", v)}
          placeholder="Apto 42"
          required={false}
        />
      </div>

      {apiError && (
        <p className="text-sm text-red-500 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
          {apiError}
        </p>
      )}

      <button
        type="submit"
        disabled={loading}
        className="h-11 w-full rounded-lg bg-orange text-white font-semibold text-sm hover:bg-orange/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors cursor-pointer"
      >
        {loading ? "Cadastrando…" : "Cadastrar"}
      </button>
    </form>
  );
};

const SectionDivider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => (
  <div className="flex items-center gap-2 pt-1">
    <span className="text-[11px] font-bold text-slate-400 uppercase tracking-widest whitespace-nowrap">
      {children}
    </span>
    <div className="flex-1 h-px bg-slate-100" />
  </div>
);

export default PersonForm;
