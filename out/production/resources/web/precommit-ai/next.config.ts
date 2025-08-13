import type { NextConfig } from "next";

/** @type {import('next').NextConfig} */
const nextConfig: NextConfig = {
  output: "export", // for `next export` (static sites)
  reactStrictMode: true,
  assetPrefix: "",
  trailingSlash: true,
  webpack: (config) => {
    config.optimization.splitChunks = false;
    config.optimization.runtimeChunk = false;
    return config;
  },
};

export default nextConfig;
