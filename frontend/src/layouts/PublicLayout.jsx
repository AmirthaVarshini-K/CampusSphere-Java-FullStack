import { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Navbar from '../components/Navbar';
import Footer from '../components/Footer';
import { PUBLIC_NAV_ITEMS } from '../constants/navigation';

export default function PublicLayout() {
  const location = useLocation();

  useEffect(() => {
    if (!location.hash) {
      return;
    }

    const target = document.querySelector(location.hash);
    if (!target) {
      return;
    }

    window.requestAnimationFrame(() => {
      const offset = 96;
      const top = target.getBoundingClientRect().top + window.scrollY - offset;
      window.scrollTo({ top, behavior: 'smooth' });
      if (typeof target.focus === 'function') {
        target.setAttribute('tabindex', '-1');
        target.focus({ preventScroll: true });
      }
    });
  }, [location.hash, location.pathname]);

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
