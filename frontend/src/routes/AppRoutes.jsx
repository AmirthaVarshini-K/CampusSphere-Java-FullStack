import React, { Suspense, lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import PublicLayout from '../layouts/PublicLayout';
import AuthLayout from '../layouts/AuthLayout';
import DashboardLayout from '../layouts/DashboardLayout';
import ProtectedRoute from './ProtectedRoute';
import { APP_ROUTES } from '../constants/routes';
import LoadingSkeleton from '../components/LoadingSkeleton';

const PlatformLandingPage = lazy(() => import('../pages/PlatformLandingPage'));
const LoginPage = lazy(() => import('../pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('../pages/auth/RegisterPage'));
const ForgotPasswordPage = lazy(() => import('../pages/auth/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('../pages/auth/ResetPasswordPage'));
const UnauthorizedPage = lazy(() => import('../pages/auth/UnauthorizedPage'));
const AccessDeniedPage = lazy(() => import('../pages/auth/AccessDeniedPage'));
const StudentProfileSetupPage = lazy(() => import('../pages/auth/StudentProfileSetupPage'));
const DashboardShellPage = lazy(() => import('../pages/DashboardShellPage'));
const DashboardPreviewPage = lazy(() => import('../pages/DashboardPreviewPage'));
const InstitutionSetupPage = lazy(() => import('../pages/InstitutionSetupPage'));
const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

function RouteLoader() {
  return (
    <div className="route-loader">
      <LoadingSkeleton lines={3} />
    </div>
  );
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<RouteLoader />}>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route path={APP_ROUTES.home} element={<PlatformLandingPage />} />
          <Route path={APP_ROUTES.unauthorized} element={<UnauthorizedPage />} />
          <Route path={APP_ROUTES.accessDenied} element={<AccessDeniedPage />} />
          <Route path="/404" element={<NotFoundPage />} />
        </Route>
        <Route element={<AuthLayout />}>
          <Route path={APP_ROUTES.login} element={<LoginPage />} />
          <Route path={APP_ROUTES.register} element={<RegisterPage />} />
          <Route path={APP_ROUTES.forgotPassword} element={<ForgotPasswordPage />} />
          <Route path={APP_ROUTES.resetPassword} element={<ResetPasswordPage />} />
        </Route>
        <Route path={APP_ROUTES.auth} element={<Navigate to={APP_ROUTES.login} replace />} />
        <Route path={`${APP_ROUTES.auth}/*`} element={<Navigate to={APP_ROUTES.login} replace />} />
        <Route path="/profile/setup" element={<StudentProfileSetupPage />} />
        <Route path="/dashboard-preview" element={<DashboardPreviewPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path={APP_ROUTES.dashboard} element={<DashboardLayout />}>
            <Route index element={<DashboardShellPage />} />
            <Route path="institution-setup" element={<InstitutionSetupPage />} />
            <Route path="institution-setup/*" element={<InstitutionSetupPage />} />
            <Route path="*" element={<DashboardShellPage />} />
          </Route>
        </Route>
        <Route path="/dashboard" element={<Navigate to={APP_ROUTES.dashboard} replace />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}
