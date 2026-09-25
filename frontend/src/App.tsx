import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import { Layout } from './components/Layout'
import { ToastProvider } from './components/Toast'
import { Chapas } from './pages/Chapas'
import { Estoque } from './pages/Estoque'
import { Login } from './pages/Login'
import { Ordens } from './pages/Ordens'
import { Painel } from './pages/Painel'

function Rotas() {
  const { usuario } = useAuth()

  if (!usuario) return <Login />

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Painel />} />
        <Route path="ordens" element={<Ordens />} />
        <Route path="chapas" element={<Chapas />} />
        <Route path="estoque" element={<Estoque />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Rotas />
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  )
}
