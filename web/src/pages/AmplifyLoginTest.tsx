import { useState } from "react";
import { signIn, signUp, fetchAuthSession } from "aws-amplify/auth";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { getApiBaseUrl } from "@/config";

export default function AmplifyLoginTest() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setMessage("Prüfe Login...");
      const response = await signIn({ username, password });
      console.log("SignIn Response:", response);

      setMessage("Login erfolgreich! Hole Token und rufe API auf...");

      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString() || session.tokens?.accessToken?.toString();
      console.log("Fetched Session:", session);
      console.log("Using Token:", token);
      const apiResponse = await fetch(`${getApiBaseUrl()}/api/v1/hello`, {
        headers: {
          Authorization: token
        }
      });

      const data = await apiResponse.text();
      setMessage(`API Response (${apiResponse.status}): ${data}`);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error("Login Error:", err);
      setMessage(`Fehler: ${err.message || err}`);
    }
  };

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setMessage("Erstelle Account...");
      // !!!!!! username is für immer fix nicht wie preferred_username, am besten uuid 
      // damit nicht verwirrend
      const response = await signUp({
        username,
        password,
        options: {
          userAttributes: { // ignore username and set uuid as email instead of username
            email: username,
            temp: "a"
          }
        }
      });
      console.log("SignUp Response:", response);
      setMessage("SignUp erfolgreich! (Näheres in Console)");
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error("SignUp Error:", err);
      setMessage(`Fehler: ${err.message || err}`);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4 bg-slate-50">
      <div className="w-full max-w-sm p-6 bg-white rounded-lg shadow-md border border-slate-200">
        <h2 className="mb-4 text-xl font-bold text-center text-slate-800">LocalStack Amplify Test</h2>
        <form className="flex flex-col gap-4">
          <Input
            placeholder="Username / Email"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <Input
            type="password"
            placeholder="Passwort"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <div className="flex gap-2">
            <Button type="button" onClick={handleLogin} className="flex-1">
              Login
            </Button>
            <Button type="button" variant="outline" onClick={handleSignUp} className="flex-1">
              Sign Up
            </Button>
          </div>
        </form>
        {message && (
          <div className="mt-4 p-3 text-sm text-center bg-slate-100 rounded text-slate-700">
            {message}
          </div>
        )}
      </div>
    </div>
  );
}
