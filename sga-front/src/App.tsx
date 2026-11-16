import { Route, Routes } from 'react-router-dom';
import { Protegida } from './components/Comunes';
import { Layout } from './components/Layout';
import Buscar from './pages/Buscar';
import Catalogo from './pages/Catalogo';
import DomoDetalle from './pages/DomoDetalle';
import Ingresar from './pages/Ingresar';
import Inicio from './pages/Inicio';
import MisReservas from './pages/MisReservas';
import NoEncontrada from './pages/NoEncontrada';
import Panel from './pages/Panel';
import Preparacion from './pages/Preparacion';
import Registro from './pages/Registro';
import Reservar from './pages/Reservar';
import Usuarios from './pages/Usuarios';
import ReservaDetalle from './pages/ReservaDetalle';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Inicio />} />
        <Route path="domos" element={<Catalogo />} />
        <Route path="domos/:id" element={<DomoDetalle />} />
        <Route path="buscar" element={<Buscar />} />
        <Route path="ingresar" element={<Ingresar />} />
        <Route path="registro" element={<Registro />} />
        <Route
          path="reservar/:id"
          element={
            <Protegida roles={['HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR']}>
              <Reservar />
            </Protegida>
          }
        />
        <Route
          path="mis-reservas"
          element={
            <Protegida roles={['HUESPED']}>
              <MisReservas />
            </Protegida>
          }
        />
        <Route
          path="reservas/:codigo"
          element={
            <Protegida roles={['HUESPED', 'RECEPCIONISTA', 'ADMINISTRADOR']}>
              <ReservaDetalle />
            </Protegida>
          }
        />
        <Route
          path="panel"
          element={
            <Protegida roles={['RECEPCIONISTA', 'ADMINISTRADOR']}>
              <Panel />
            </Protegida>
          }
        />
        <Route
          path="preparacion"
          element={
            <Protegida roles={['PERSONAL_SERVICIO', 'ADMINISTRADOR']}>
              <Preparacion />
            </Protegida>
          }
        />
        <Route
          path="usuarios"
          element={
            <Protegida roles={['ADMINISTRADOR']}>
              <Usuarios />
            </Protegida>
          }
        />
        <Route path="*" element={<NoEncontrada />} />
      </Route>
    </Routes>
  );
}
