import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { userApi } from '../../services/userApi';
import { useAuth } from '../../context/AuthContext';
import AuthCard from '../../components/AuthCard';
import FormSection from '../../components/FormSection';
import LoadingButton from '../../components/LoadingButton';
import { ErrorBanner, SuccessBanner } from '../../components/Banner';
import ProfileCard from '../../components/ProfileCard';
import { APP_ROUTES } from '../../constants/routes';
import Button from '../../components/Button';

export default function StudentProfileSetupPage() {
  const { user, signIn, accessToken, refreshToken } = useAuth();
  const [profile, setProfile] = useState(() => user ?? {});
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    let mounted = true;
    async function loadProfile() {
      setLoading(true);
      try {
        const response = await userApi.me();
        if (mounted) {
          setProfile(response.data.data);
        }
      } catch (error) {
        if (mounted) {
          setErrorMessage(error?.response?.data?.message ?? 'Unable to load profile data.');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    if (accessToken || refreshToken) {
      loadProfile();
    }

    return () => {
      mounted = false;
    };
  }, [accessToken, refreshToken]);

  const completion = useMemo(() => calculateCompletion(profile), [profile]);

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setErrorMessage('');
    setSuccessMessage('');
    try {
      const response = await userApi.updateProfile({
        firstName: profile.firstName,
        lastName: profile.lastName,
        department: profile.department,
        academicYear: profile.academicYear,
        section: profile.section,
        phoneNumber: profile.phoneNumber,
        profilePictureUrl: profile.profilePictureUrl
      });
      setProfile(response.data.data);
      signIn({ accessToken, refreshToken, user: response.data.data });
      setSuccessMessage('Profile saved successfully.');
    } catch (error) {
      setErrorMessage(error?.response?.data?.message ?? 'Unable to save the profile.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <AuthCard title="Profile setup" description="Loading your account profile.">
        <div className="state-message">
          <p>Please wait while we fetch your details.</p>
        </div>
      </AuthCard>
    );
  }

  return (
    <AuthCard title="Profile setup" description="Finish your CampusSphere profile before entering the portal.">
      <SuccessBanner message={successMessage} />
      <ErrorBanner message={errorMessage} />
      <div className="progress-pill">Completion {completion}%</div>
      <ProfileCard user={profile} />
      <form className="auth-form auth-form--stacked" onSubmit={handleSubmit}>
        <FormSection title="Personal details">
          <div className="grid grid--2">
            <label className="field" htmlFor="firstName">
              <span className="field__label">First name</span>
              <input id="firstName" className="input" value={profile?.firstName ?? ''} onChange={e => setProfile({ ...profile, firstName: e.target.value })} />
            </label>
            <label className="field" htmlFor="lastName">
              <span className="field__label">Last name</span>
              <input id="lastName" className="input" value={profile?.lastName ?? ''} onChange={e => setProfile({ ...profile, lastName: e.target.value })} />
            </label>
          </div>
        </FormSection>
        <FormSection title="Academic information">
          <div className="grid grid--3">
            <label className="field" htmlFor="department">
              <span className="field__label">Department</span>
              <input id="department" className="input" value={profile?.department ?? ''} onChange={e => setProfile({ ...profile, department: e.target.value })} />
            </label>
            <label className="field" htmlFor="academicYear">
              <span className="field__label">Year</span>
              <input id="academicYear" className="input" value={profile?.academicYear ?? ''} onChange={e => setProfile({ ...profile, academicYear: e.target.value })} />
            </label>
            <label className="field" htmlFor="section">
              <span className="field__label">Section</span>
              <input id="section" className="input" value={profile?.section ?? ''} onChange={e => setProfile({ ...profile, section: e.target.value })} />
            </label>
          </div>
        </FormSection>
        <FormSection title="Contact and avatar">
          <div className="grid grid--2">
            <label className="field" htmlFor="phoneNumber">
              <span className="field__label">Phone number</span>
              <input id="phoneNumber" className="input" value={profile?.phoneNumber ?? ''} onChange={e => setProfile({ ...profile, phoneNumber: e.target.value })} />
            </label>
            <label className="field" htmlFor="profilePictureUrl">
              <span className="field__label">Profile picture URL</span>
              <input id="profilePictureUrl" className="input" value={profile?.profilePictureUrl ?? ''} onChange={e => setProfile({ ...profile, profilePictureUrl: e.target.value })} />
            </label>
          </div>
        </FormSection>
        <LoadingButton loading={saving} type="submit" fullWidth>
          Save profile
        </LoadingButton>
      </form>
      <div className="auth-card__footer">
        <Link to={APP_ROUTES.dashboard}>Continue to dashboard</Link>
        <Link to={APP_ROUTES.login}>Switch account</Link>
      </div>
      <div className="auth-card__subnote">
        <Button variant="secondary" as={Link} to={APP_ROUTES.home} size="sm">
          Back to overview
        </Button>
      </div>
    </AuthCard>
  );
}

function calculateCompletion(profile) {
  if (!profile) {
    return 0;
  }
  const fields = ['firstName', 'lastName', 'email', 'department', 'academicYear', 'section', 'phoneNumber', 'profilePictureUrl'];
  const filled = fields.filter(field => String(profile[field] ?? '').trim().length > 0).length;
  return Math.round((filled / fields.length) * 100);
}
