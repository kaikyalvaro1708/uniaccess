import React, { useState } from "react";
import LeftPanel from "./components/LeftPanel";
import PersonForm from "./components/PersonForm";
import SuccessCard from "./components/SuccessCard";
import LoginForm from "./components/LoginForm";
import WelcomeCard from "./components/WelcomeCard";
import backgroundImg from "./img/background.png";
import logoUniAccess from "./img/logo_uniaccess.png";
import type { PersonDetailsData } from "./types/person";

type RightView = "register" | "login";

const App: React.FC = () => {
  const [rightView,  setRightView]  = useState<RightView>("register");
  const [registered, setRegistered] = useState<PersonDetailsData | null>(null);
  const [loggedIn,   setLoggedIn]   = useState<PersonDetailsData | null>(null);

  const goToLogin = () => {
    setRightView("login");
    setRegistered(null);
  };

  const goToRegister = () => {
    setRightView("register");
    setLoggedIn(null);
  };

  const handleRegisterSuccess = (person: PersonDetailsData) => {
    setRegistered(person);
    setRightView("register");
  };

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{
        backgroundImage: `url(${backgroundImg})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        backgroundAttachment: "fixed",
      }}
    >
      {/* card principal */}
      <div className="w-full max-w-4xl rounded-2xl overflow-hidden shadow-2xl grid grid-cols-1 lg:grid-cols-[2fr_3fr]">

        {/* painel esquerdo com imagem */}
        <LeftPanel onLoginClick={goToLogin} />

        {/* painel direito branco */}
        <div className="bg-white p-8 lg:p-10 overflow-y-auto max-h-screen lg:max-h-[90vh]">

          {/* logo topo-direito — oculta na tela logada (WelcomeCard já tem a logo) */}
          {!(rightView === "login" && loggedIn) && (
            <div className="flex justify-end mb-4">
              <img src={logoUniAccess} alt="UniAccess" className="h-6 object-contain" />
            </div>
          )}



          {rightView === "register" && !registered && (
            <>
              <div className="mb-6">
                <h2 className="text-xl font-bold text-slate-800">
                  Novo cadastro
                </h2>
                <p className="text-sm text-slate-500 mt-1">
                  Preencha os dados abaixo. Um login será gerado automaticamente.
                </p>
              </div>
              <PersonForm onSuccess={handleRegisterSuccess} />
            </>
          )}

          {rightView === "register" && registered && (
            <SuccessCard
              person={registered}
              onReset={() => setRegistered(null)}
            />
          )}

          {rightView === "login" && !loggedIn && (
            <LoginForm onSuccess={setLoggedIn} onBack={goToRegister} />
          )}

          {rightView === "login" && loggedIn && (
            <WelcomeCard
              person={loggedIn}
              onLogout={() => setLoggedIn(null)}
            />
          )}

        </div>
      </div>
    </div>
  );
};

export default App;
