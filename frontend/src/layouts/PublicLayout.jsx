import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { PUBLIC_NAV_ITEMS } from '../constants/navigation';

export default function PublicLayout() {
  return (
    <div className="public-shell page-shell page-shell--public">
      <Navbar variant="public" publicNavItems={PUBLIC_NAV_ITEMS} />
      <main className="page-shell__content public-shell__content">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
