"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import AuthService from "../services/AuthService";
import Navbar from "./Navbar";

export default function LoginForm() {
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.username || !form.password) {
      return setError("All fields are required.");
    }

    try {
      setLoading(true);
      await AuthService.login(form); // Call API service
      router.push("/dashboard"); // Redirect after login
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const labelClasses = (value) =>
    `absolute left-2 transition-all bg-white px-1 ${
      value
        ? "top-[-8px] text-xs text-blue-500"
        : "peer-focus:top-[-8px] peer-focus:text-xs peer-focus:text-blue-500 top-2.5 text-gray-400 text-base"
    }`;

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <div className="flex justify-center items-center py-10">
        <form
          onSubmit={handleSubmit}
          className="bg-white shadow-lg rounded-lg p-6 w-full max-w-md"
        >
          <h2 className="text-2xl font-bold text-center mb-4">Login</h2>

          {error && (
            <p className="text-red-500 text-sm bg-red-100 p-2 rounded mb-3">
              {error}
            </p>
          )}

          {/* Username */}
          <div className="relative mb-3">
            <input
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="username" className={labelClasses(form.username)}>
              User Name
            </label>
          </div>

          {/* Password */}
          <div className="relative mb-4">
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="password" className={labelClasses(form.password)}>
              Password
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
            {loading ? "Logging in..." : "Login"}
          </button>

          <p className="text-sm text-center mt-4">
            Don’t have an account?{" "}
            <a href="/register" className="text-blue-600 hover:underline">
              Register
            </a>
          </p>
          <p className="text-sm text-center mt-4">
            Lost Your Password?{" "}
            <a href="/forgot-password" className="text-blue-600 hover:underline">
              Forgot Password
            </a>
          </p>
        </form>
      </div>
    </div>
  );
}
