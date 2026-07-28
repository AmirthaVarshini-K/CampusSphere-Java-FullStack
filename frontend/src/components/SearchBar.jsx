import Input from './Input';

export default function SearchBar({ value, onChange, placeholder = 'Search' }) {
  return (
    <Input
      id="global-search"
      value={value}
      onChange={event => onChange(event.target.value)}
      placeholder={placeholder}
      aria-label={placeholder}
    />
  );
}
