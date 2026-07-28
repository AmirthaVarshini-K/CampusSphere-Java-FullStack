import React from 'react';
import { Link } from 'react-router-dom';
import Button from './Button';
import Card from './Card';

export default class AppErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    if (process.env.NODE_ENV !== 'production') {
      // Development diagnostics only.
      console.error('CampusSphere render error', error, errorInfo);
    }
  }

  handleRetry = () => {
    this.setState({ hasError: false });
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="page-shell page-shell--public">
          <main className="page-shell__content">
            <Card className="error-boundary" elevated>
              <p className="eyebrow">CampusSphere</p>
              <h1>We hit an unexpected issue.</h1>
              <p>
                The application could not render this view. You can retry or return to the login screen and continue from there.
              </p>
              <div className="button-row">
                <Button onClick={this.handleRetry}>Try Again</Button>
                <Button as={Link} variant="secondary" to="/login">
                  Return to Login
                </Button>
              </div>
            </Card>
          </main>
        </div>
      );
    }

    return this.props.children;
  }
}
