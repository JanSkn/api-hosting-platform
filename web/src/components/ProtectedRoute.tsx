import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { fetchAuthSession } from "aws-amplify/auth";

type AuthState = "loading" | "authenticated" | "unauthenticated";

export default function ProtectedRoute() {
  const [authState, setAuthState] = useState<AuthState>("loading");

  useEffect(() => {
    fetchAuthSession()
      .then((session) => {
        const isAuthenticated =
          !!session.tokens?.accessToken || !!session.tokens?.idToken;
        setAuthState(isAuthenticated ? "authenticated" : "unauthenticated");
      })
      .catch(() => {
        setAuthState("unauthenticated");
      });
  }, []);

  if (authState === "loading") {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin" />
          <p className="text-sm text-muted-foreground">Authenticating…</p>
        </div>
      </div>
    );
  }

  if (authState === "unauthenticated") {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
