"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import AuthService from "./services/AuthService";
import LoginForm from "./components/LoginForm";

export default function HomePage() {
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const checkAuth = async () => {
      try {
        const user = await AuthService.getCurrentUser();
        if (user) {
          router.push("/dashboard"); // Redirect logged-in users
        }
      } catch {
        // Not logged in → show login
      } finally {
        setLoading(false);
      }
    };
    checkAuth();
  }, [router]);

  if (loading) {
    return (
      <div className="min-h-screen flex justify-center items-center">
        <p className="text-gray-600 text-lg">Loading...</p>
      </div>
    );
  }

  return <LoginForm />; // Show login form if not logged in
}
