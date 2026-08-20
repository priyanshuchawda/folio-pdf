export function DevicePreview() {
  return (
    <div
      className="anim-rise-delay relative mx-auto w-full max-w-[340px] lg:max-w-none"
      data-testid="device-preview"
    >
      <div
        aria-hidden
        className="anim-drift absolute -inset-8 rounded-[2rem] opacity-70 blur-2xl"
        style={{
          background: "radial-gradient(circle at 40% 30%, var(--glow), transparent 60%)",
        }}
      />
      <div className="relative overflow-hidden rounded-[1.6rem] border border-[var(--line)] bg-[linear-gradient(160deg,#141c17_0%,#0e1411_100%)] shadow-[0_30px_80px_rgba(0,0,0,0.45)]">
        <div className="flex items-center justify-between border-b border-[var(--line)] px-4 py-3">
          <span className="text-xs text-[var(--muted)]">cn_networks.pdf</span>
          <span className="font-display text-xs font-semibold text-[var(--leaf)]">142 / 1831</span>
        </div>
        <div className="relative aspect-[3/4] bg-[linear-gradient(180deg,#1a221c_0%,#121914_100%)] p-5">
          <div className="h-full rounded-md border border-[var(--line)] bg-[#f3f0e6] p-4 text-[#1c1a14] shadow-inner">
            <div className="mb-3 h-2 w-1/3 rounded bg-[#cfc8b4]/60" />
            <div className="space-y-2">
              <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[92%] rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[86%] rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[78%] rounded bg-[#d6d0be]" />
            </div>
            <div className="mt-6 space-y-2 opacity-80">
              <div className="h-1.5 w-full rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[90%] rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[95%] rounded bg-[#d6d0be]" />
              <div className="h-1.5 w-[70%] rounded bg-[#d6d0be]" />
            </div>
          </div>
          <div className="absolute right-3 top-1/3 flex h-16 w-11 items-center justify-center rounded-l-xl bg-[#1b3a2f]/95 text-center font-display text-[0.65rem] font-bold leading-tight text-white shadow-lg">
            142
            <br />
            <span className="font-normal opacity-70">/1831</span>
          </div>
        </div>
        <div className="border-t border-[var(--line)] px-4 py-3 text-center text-sm text-[var(--mist)]">
          142 / 1831 · tap to go
        </div>
      </div>
    </div>
  );
}
