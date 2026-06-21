import {
  signIn,
  signUp,
  signOut,
  fetchAuthSession,
  fetchUserAttributes,
} from "aws-amplify/auth";
import { apiFetch } from "./apiClient";
import { clearLogCache } from "@/utils/logCache";

export interface User {
  id: string;
  name: string;
  email: string;
}

export async function login(email: string, password: string): Promise<User> {
  // Clear any lingering session first; Amplify's signIn throws
  // "There is already a signed in user" otherwise, which would silently
  // leave the previous user's session active.
  clearLogCache();
  await signOut();
  await signIn({ username: email, password });
  return getCurrentUser() as Promise<User>;
}

export async function register(
  name: string,
  email: string,
  password: string
): Promise<User> {
  clearLogCache();
  await signUp({
    username: email,
    password,
    options: {
      userAttributes: {
        email,
        name,
      },
    },
  });
  // After sign-up the user may need to confirm their email;
  // attempt sign-in so the session is active for the caller.
  // Clear any lingering session first so signIn doesn't throw
  // "There is already a signed in user".
  await signOut();
  await signIn({ username: email, password });
  return getCurrentUser() as Promise<User>;
}

export async function logout(): Promise<void> {
  clearLogCache();
  await signOut();
}

export async function getCurrentUser(): Promise<User | null> {
  try {
    const session = await fetchAuthSession();
    if (!session.tokens?.idToken && !session.tokens?.accessToken) {
      return null;
    }
    const attrs = await fetchUserAttributes();
    return {
      id: attrs.sub ?? "",
      name: attrs.name ?? attrs.email ?? "",
      email: attrs.email ?? "",
    };
  } catch {
    return null;
  }
}

export async function updateProfile(_data: { name: string }): Promise<User> {
  // Profile updates are not exposed by the current backend.
  // Return the current user as-is.
  const user = await getCurrentUser();
  if (!user) throw new Error("Not authenticated");
  return user;
}

export async function deleteAccount(): Promise<void> {
  clearLogCache();
  await apiFetch("/users/me", { method: "DELETE" });
  await signOut();
}
