"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import AuthService from "../services/AuthService";
import Navbar from "./Navbar";

export default function RegisterForm() {
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    name: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  // Validate password
  const validatePassword = (password, confirmPassword) => {
    const passwordRegex = /^(?=.*[A-Z])(?=.*[@#$%?!])[A-Za-z\d@#$%?!]{8,}$/; // 1 uppercase + 1 special char + min 8 chars
    if (password !== confirmPassword) {
      return "Passwords do not match.";
    }
    if (!passwordRegex.test(password)) {
      return "Password must be 8+ chars, include 1 uppercase & 1 special char.";
    }
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // Check required fields
    if (
      !form.username ||
      !form.email ||
      !form.password ||
      !form.confirmPassword ||
      !form.name
    ) {
      return setError("All fields are required.");
    }

    // Validate password
    const passwordError = validatePassword(form.password, form.confirmPassword);
    if (passwordError) return setError(passwordError);

    try {
      setLoading(true);
      await AuthService.register({
        username: form.username,
        email: form.email,
        password: form.password,
        name: form.name,
      });

      router.push("/login"); // Redirect after registration
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
          <h2 className="text-2xl font-bold text-center mb-4">Register</h2>

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
              Username
            </label>
          </div>

          {/* Email */}
          <div className="relative mb-3">
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="email" className={labelClasses(form.email)}>
              Email ID
            </label>
          </div>

          {/* Password */}
          <div className="relative mb-3">
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

          {/* Confirm Password */}
          <div className="relative mb-3">
            <input
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label
              htmlFor="confirmPassword"
              className={labelClasses(form.confirmPassword)}
            >
              Confirm Password
            </label>
          </div>

          {/* Name */}
          <div className="relative mb-4">
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              className="peer border p-2 w-full rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <label htmlFor="name" className={labelClasses(form.name)}>
              Name
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
            {loading ? "Registering..." : "Register"}
          </button>

          <p className="text-sm text-center mt-4">
            Already have an account?{" "}
            <a href="/login" className="text-blue-600 hover:underline">
              Login
            </a>
          </p>
        </form>
      </div>
    </div>
  );
}
