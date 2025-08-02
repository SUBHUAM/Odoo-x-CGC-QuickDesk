"use client";
import { useState } from "react";
import Navbar from "../components/Navbar";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Email validation function
  const isEmailValid = (email) => {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    // Validation
    if (!email) {
      return setError("Email is required.");
    }
    if (!isEmailValid(email)) {
      return setError("Please enter a valid email address.");
    }

    try {
      setLoading(true);

      // API request to trigger password reset (dummy for now)
      const res = await fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Something went wrong");

      setMessage("Password reset instructions sent to your email.");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Floating label classes
  const labelClasses = (value) =>
    `absolute left-2 transition-all bg-white px-1
    ${value
      ? "top-[-8px] text-xs text-blue-500"
      : "peer-focus:top-[-8px] peer-focus:text-xs peer-focus:text-blue-500 top-2.5 text-gray-400 text-base"
    }`;

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navbar */}
      <Navbar />

      {/* Forgot Password Form */}
      <div className="flex justify-center items-center py-10">
        <form
          onSubmit={handleSubmit}
          className="bg-white shadow-lg rounded-lg p-6 w-full max-w-md"
        >
          <h2 className="text-2xl font-bold text-center mb-4">
            Forgot Password
          </h2>

          {error && (
            <p className="text-red-500 text-sm bg-red-100 p-2 rounded mb-3">
              {error}
            </p>
          )}
          {message && (
            <p className="text-green-500 text-sm bg-green-100 p-2 rounded mb-3">
              {message}
            </p>
          )}

          {/* Email with floating label */}
          <div className="relative mb-4">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="email" className={labelClasses(email)}>
              Enter your registered email
            </label>
          </div>

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2 text-white rounded ${
              loading
                ? "bg-blue-300 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            {loading ? "Sending..." : "Send Reset Link"}
          </button>

          <p className="text-sm text-center mt-4">
            <a href="/login" className="text-blue-600 hover:underline">
              Back to Login
            </a>
          </p>
        </form>
      </div>
    </div>
  );
}
