export default function OtpInput({ length = 6, value = '', onChange }) {
  const digits = Array.from({ length }, (_, index) => value[index] ?? '');

  return (
    <div className="otp-input" aria-label={`${length}-digit token input`}>
      {digits.map((digit, index) => (
        <input
          key={index}
          className="otp-input__cell"
          value={digit}
          inputMode="numeric"
          maxLength={1}
          onChange={event => {
            const next = value.split('');
            next[index] = event.target.value.slice(-1);
            onChange(next.join('').slice(0, length));
          }}
        />
      ))}
    </div>
  );
}
